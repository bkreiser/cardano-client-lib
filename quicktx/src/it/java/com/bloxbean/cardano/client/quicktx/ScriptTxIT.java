package com.bloxbean.cardano.client.quicktx;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.address.AddressProvider;
import com.bloxbean.cardano.client.api.UtxoSupplier;
import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.api.DefaultUtxoSupplier;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.function.helper.ScriptUtxoFinders;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.plutus.spec.BigIntPlutusData;
import com.bloxbean.cardano.client.plutus.spec.PlutusData;
import com.bloxbean.cardano.client.plutus.spec.PlutusV2Script;
import com.bloxbean.cardano.client.transaction.spec.Asset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScriptTxIT extends QuickTxBaseIT {
    BackendService backendService;
    UtxoSupplier utxoSupplier;
    Account sender1;
    Account sender2;
    String sender1Addr;
    String sender2Addr;

    String receiver1 = "addr_test1qz3s0c370u8zzqn302nppuxl840gm6qdmjwqnxmqxme657ze964mar2m3r5jjv4qrsf62yduqns0tsw0hvzwar07qasqeamp0c";
    String receiver2 = "addr_test1qqwpl7h3g84mhr36wpetk904p7fchx2vst0z696lxk8ujsjyruqwmlsm344gfux3nsj6njyzj3ppvrqtt36cp9xyydzqzumz82";
    String receiver3 = "addr_test1qqqvjp4ffcdqg3fmx0k8rwamnn06wp8e575zcv8d0m3tjn2mmexsnkxp7az774522ce4h3qs4tjp9rxjjm46qf339d9sk33rqn";

    QuickTxBuilder quickTxBuilder;

    @BeforeEach
    void setup() {
        backendService = getBackendService();
        utxoSupplier = new DefaultUtxoSupplier(backendService.getUtxoService());
        quickTxBuilder = new QuickTxBuilder(backendService);

        //addr_test1qp73ljurtknpm5fgey5r2y9aympd33ksgw0f8rc5khheg83y35rncur9mjvs665cg4052985ry9rzzmqend9sqw0cdksxvefah
        String senderMnemonic = "drive useless envelope shine range ability time copper alarm museum near flee wrist live type device meadow allow churn purity wisdom praise drop code";
        sender1 = new Account(Networks.testnet(), senderMnemonic);
        sender1Addr = sender1.baseAddress();

        //addr_test1qz5fcpvkg7pekqvv9ld03t5sx2w2c2fac67fzlaxw5844s83l4p6tr389lhgcpe4797kt7xkcxqvcc4a6qjshzsmta8sh3ncs4
        String sender2Mnemonic = "access else envelope between rubber celery forum brief bubble notice stomach add initial avocado current net film aunt quick text joke chase robust artefact";
        sender2 = new Account(Networks.testnet(), sender2Mnemonic);
        sender2Addr = sender2.baseAddress();
    }

    @Test
    void alwaysTrueScript() throws ApiException, InterruptedException {
        PlutusV2Script plutusScript = PlutusV2Script.builder()
                .type("PlutusScriptV2")
                .cborHex("49480100002221200101")
                .build();

        String scriptAddress = AddressProvider.getEntAddress(plutusScript, Networks.testnet()).toBech32();
        BigInteger scriptAmt = new BigInteger("2479280");

        Random rand = new Random();
        int randInt = rand.nextInt();
        BigIntPlutusData plutusData =  new BigIntPlutusData(BigInteger.valueOf(randInt)); //any random number

        Tx tx = new Tx();
        tx.payToContract(scriptAddress, Amount.lovelace(scriptAmt), plutusData)
                .from(sender2Addr);

        QuickTxBuilder quickTxBuilder = new QuickTxBuilder(backendService);
        Result<String> result = quickTxBuilder.compose(tx)
                .withSigner(SignerProviders.signerFrom(sender2))
                .completeAndWait(System.out::println);

        System.out.println(result.getResponse());
        checkIfUtxoAvailable(result.getValue(), scriptAddress);

        Optional<Utxo> optionalUtxo  = ScriptUtxoFinders.findFirstByInlineDatum(utxoSupplier, scriptAddress, plutusData);
        ScriptTx scriptTx = new ScriptTx()
                .collectFrom(optionalUtxo.get(), plutusData)
                .payToAddress(receiver1, Amount.lovelace(scriptAmt))
                .attachSpendingValidator(plutusScript)
                .withChangeAddress(scriptAddress, plutusData);

        Result<String> result1 = quickTxBuilder.compose(scriptTx)
                .feePayer(sender1Addr)
                .withSigner(SignerProviders.signerFrom(sender1))
                .completeAndWait(System.out::println);

        System.out.println(result1.getResponse());
        assertTrue(result1.isSuccessful());
    }


    @Test
    void alwaysTrueScript_guessSumScript() throws ApiException, InterruptedException {
        PlutusV2Script alwaysTrueScript = PlutusV2Script.builder()
                .type("PlutusScriptV2")
                .cborHex("49480100002221200101")
                .build();

        String alwaysTrueScriptAddr = AddressProvider.getEntAddress(alwaysTrueScript, Networks.testnet()).toBech32();
        Amount alwaysTrueScriptAmt = Amount.lovelace(BigInteger.valueOf(2479280));

        Random rand = new Random();
        int randInt = rand.nextInt();
        BigIntPlutusData alwaysTruePlutusData =  new BigIntPlutusData(BigInteger.valueOf(randInt)); //any random number

        //Sum Script
        PlutusV2Script sumScript =
                PlutusV2Script.builder()
                        .cborHex("5907a65907a3010000323322323232323232323232323232323322323232323222232325335323232333573466e1ccc07000d200000201e01d3333573466e1cd55cea80224000466442466002006004646464646464646464646464646666ae68cdc39aab9d500c480008cccccccccccc88888888888848cccccccccccc00403403002c02802402001c01801401000c008cd405c060d5d0a80619a80b80c1aba1500b33501701935742a014666aa036eb94068d5d0a804999aa80dbae501a35742a01066a02e0446ae85401cccd5406c08dd69aba150063232323333573466e1cd55cea801240004664424660020060046464646666ae68cdc39aab9d5002480008cc8848cc00400c008cd40b5d69aba15002302e357426ae8940088c98c80c0cd5ce01881801709aab9e5001137540026ae854008c8c8c8cccd5cd19b8735573aa004900011991091980080180119a816bad35742a004605c6ae84d5d1280111931901819ab9c03103002e135573ca00226ea8004d5d09aba2500223263202c33573805a05805426aae7940044dd50009aba1500533501775c6ae854010ccd5406c07c8004d5d0a801999aa80dbae200135742a00460426ae84d5d1280111931901419ab9c029028026135744a00226ae8940044d5d1280089aba25001135744a00226ae8940044d5d1280089aba25001135744a00226ae8940044d55cf280089baa00135742a00860226ae84d5d1280211931900d19ab9c01b01a018375a00a6eb4014405c4c98c805ccd5ce24810350543500017135573ca00226ea800448c88c008dd6000990009aa80b911999aab9f0012500a233500930043574200460066ae880080508c8c8cccd5cd19b8735573aa004900011991091980080180118061aba150023005357426ae8940088c98c8050cd5ce00a80a00909aab9e5001137540024646464646666ae68cdc39aab9d5004480008cccc888848cccc00401401000c008c8c8c8cccd5cd19b8735573aa0049000119910919800801801180a9aba1500233500f014357426ae8940088c98c8064cd5ce00d00c80b89aab9e5001137540026ae854010ccd54021d728039aba150033232323333573466e1d4005200423212223002004357426aae79400c8cccd5cd19b875002480088c84888c004010dd71aba135573ca00846666ae68cdc3a801a400042444006464c6403666ae7007006c06406005c4d55cea80089baa00135742a00466a016eb8d5d09aba2500223263201533573802c02a02626ae8940044d5d1280089aab9e500113754002266aa002eb9d6889119118011bab00132001355014223233335573e0044a010466a00e66442466002006004600c6aae754008c014d55cf280118021aba200301213574200222440042442446600200800624464646666ae68cdc3a800a40004642446004006600a6ae84d55cf280191999ab9a3370ea0049001109100091931900819ab9c01101000e00d135573aa00226ea80048c8c8cccd5cd19b875001480188c848888c010014c01cd5d09aab9e500323333573466e1d400920042321222230020053009357426aae7940108cccd5cd19b875003480088c848888c004014c01cd5d09aab9e500523333573466e1d40112000232122223003005375c6ae84d55cf280311931900819ab9c01101000e00d00c00b135573aa00226ea80048c8c8cccd5cd19b8735573aa004900011991091980080180118029aba15002375a6ae84d5d1280111931900619ab9c00d00c00a135573ca00226ea80048c8cccd5cd19b8735573aa002900011bae357426aae7940088c98c8028cd5ce00580500409baa001232323232323333573466e1d4005200c21222222200323333573466e1d4009200a21222222200423333573466e1d400d2008233221222222233001009008375c6ae854014dd69aba135744a00a46666ae68cdc3a8022400c4664424444444660040120106eb8d5d0a8039bae357426ae89401c8cccd5cd19b875005480108cc8848888888cc018024020c030d5d0a8049bae357426ae8940248cccd5cd19b875006480088c848888888c01c020c034d5d09aab9e500b23333573466e1d401d2000232122222223005008300e357426aae7940308c98c804ccd5ce00a00980880800780700680600589aab9d5004135573ca00626aae7940084d55cf280089baa0012323232323333573466e1d400520022333222122333001005004003375a6ae854010dd69aba15003375a6ae84d5d1280191999ab9a3370ea0049000119091180100198041aba135573ca00c464c6401866ae700340300280244d55cea80189aba25001135573ca00226ea80048c8c8cccd5cd19b875001480088c8488c00400cdd71aba135573ca00646666ae68cdc3a8012400046424460040066eb8d5d09aab9e500423263200933573801401200e00c26aae7540044dd500089119191999ab9a3370ea00290021091100091999ab9a3370ea00490011190911180180218031aba135573ca00846666ae68cdc3a801a400042444004464c6401466ae7002c02802001c0184d55cea80089baa0012323333573466e1d40052002200923333573466e1d40092000200923263200633573800e00c00800626aae74dd5000a4c240029210350543100320013550032225335333573466e1c0092000005004100113300333702004900119b80002001122002122001112323001001223300330020020011")
                        .build();
        String sumScriptAddr = AddressProvider.getEntAddress(sumScript, Networks.testnet()).toBech32();
        Amount sumScriptAmt = Amount.ada(4.0);
        PlutusData sumScriptDatum =  new BigIntPlutusData(BigInteger.valueOf(8)); //redeemer should be 36
        PlutusData sumScriptRedeemer = new BigIntPlutusData(BigInteger.valueOf(36));

        Tx tx = new Tx();
        tx.payToContract(alwaysTrueScriptAddr, alwaysTrueScriptAmt, alwaysTruePlutusData)
                .payToContract(sumScriptAddr, sumScriptAmt, sumScriptDatum)
                .from(sender2Addr);

        QuickTxBuilder quickTxBuilder = new QuickTxBuilder(backendService);
        Result<String> result = quickTxBuilder.compose(tx)
                .withSigner(SignerProviders.signerFrom(sender2))
                .completeAndWait(System.out::println);

        System.out.println(result.getResponse());

        Optional<Utxo> alwaysTrueUtxo  = ScriptUtxoFinders.findFirstByInlineDatum(utxoSupplier, alwaysTrueScriptAddr, alwaysTruePlutusData);
        Optional<Utxo> sumUtxo  = ScriptUtxoFinders.findFirstByInlineDatum(utxoSupplier, sumScriptAddr, sumScriptDatum);
//        Optional<Utxo> alwaysTrueUtxo = utxoSupplier.getTxOutput(result.getValue(), 0);
//        Optional<Utxo> sumUtxo = utxoSupplier.getTxOutput(result.getValue(), 1);
        ScriptTx scriptTx = new ScriptTx()
                .collectFrom(alwaysTrueUtxo.get(), PlutusData.unit())
                .collectFrom(sumUtxo.get(), sumScriptRedeemer)
                .payToAddress(receiver1, List.of(alwaysTrueScriptAmt, sumScriptAmt))
                .attachSpendingValidator(alwaysTrueScript)
                .attachSpendingValidator(sumScript)
                .withChangeAddress(sumScriptAddr, sumScriptDatum);

        Result<String> result1 = quickTxBuilder.compose(scriptTx)
                .feePayer(sender1Addr)
                .withSigner(SignerProviders.signerFrom(sender1))
                .completeAndWait(System.out::println);

        System.out.println(result1.getResponse());
        assertTrue(result1.isSuccessful());
    }

    @Test
    void alwaysTrueScript_withFeeFromChange_differentCollateral() throws ApiException {
        PlutusV2Script plutusScript = PlutusV2Script.builder()
                .type("PlutusScriptV2")
                .cborHex("49480100002221200101")
                .build();

        String scriptAddress = AddressProvider.getEntAddress(plutusScript, Networks.testnet()).toBech32();
        BigInteger scriptAmt = new BigInteger("3479280");

        Random rand = new Random();
        int randInt = rand.nextInt();
        BigIntPlutusData plutusData =  new BigIntPlutusData(BigInteger.valueOf(randInt)); //any random number

        Tx tx = new Tx();
        tx.payToContract(scriptAddress, Amount.lovelace(scriptAmt), plutusData)
                .from(sender2Addr);

        QuickTxBuilder quickTxBuilder = new QuickTxBuilder(backendService);
        Result<String> result = quickTxBuilder.compose(tx)
                .withSigner(SignerProviders.signerFrom(sender2))
                .completeAndWait(System.out::println);

        System.out.println(result.getResponse());

        Optional<Utxo> optionalUtxo  = ScriptUtxoFinders.findFirstByInlineDatum(utxoSupplier, scriptAddress, plutusData);
        ScriptTx scriptTx = new ScriptTx()
                .collectFrom(optionalUtxo.get(), plutusData)
                .payToAddress(receiver1, Amount.ada(1.1))
                .attachSpendingValidator(plutusScript)
                .withChangeAddress(scriptAddress, plutusData);

        Result<String> result1 = quickTxBuilder.compose(scriptTx)
                .feePayer(scriptAddress)
                .collateralPayer(sender1Addr)
                .withSigner(SignerProviders.signerFrom(sender1))
                .completeAndWait(System.out::println);

        System.out.println(result1.getResponse());
        assertTrue(result1.isSuccessful());
    }

    @Test
    void alwaysTrueScript_minting() throws ApiException, CborSerializationException, InterruptedException {
        PlutusV2Script mintingScript = PlutusV2Script.builder()
                .type("PlutusScriptV2")
                .cborHex("49480100002221200101")
                .build();

        PlutusV2Script spendingScript = PlutusV2Script.builder()
                .type("PlutusScriptV2")
                .cborHex("49480100002221200101")
                .build();

        String scriptAddress = AddressProvider.getEntAddress(spendingScript, Networks.testnet()).toBech32();
        BigInteger scriptAmt = new BigInteger("2479280");

        Random rand = new Random();
        int randInt = rand.nextInt();
        BigIntPlutusData plutusData =  new BigIntPlutusData(BigInteger.valueOf(randInt)); //any random number

        Tx tx = new Tx();
        tx.payToContract(scriptAddress, Amount.lovelace(scriptAmt), plutusData)
                .from(sender2Addr);

        QuickTxBuilder quickTxBuilder = new QuickTxBuilder(backendService);
        Result<String> result = quickTxBuilder.compose(tx)
                .withSigner(SignerProviders.signerFrom(sender2))
                .completeAndWait(System.out::println);

        System.out.println(result.getResponse());

        //Required as backend service returns outdated utxo
        if (result.isSuccessful()) {
            checkIfUtxoAvailable(result.getValue(), sender2Addr);
        }

        System.out.println("Script Addr: " + scriptAddress);
        System.out.println("Datum: " + randInt);
        Optional<Utxo> spendingUtxoOptional  = ScriptUtxoFinders.findFirstByInlineDatum(utxoSupplier, scriptAddress, plutusData);

        Asset asset = new Asset("PlutusMintToken", BigInteger.valueOf(4000));

        ScriptTx scriptTx = new ScriptTx()
              .payToAddress(receiver2, Amount.lovelace(scriptAmt))
                .collectFrom(spendingUtxoOptional.get(), plutusData)
                .mintAsset(mintingScript, asset, PlutusData.unit(), sender2Addr)
                .attachSpendingValidator(spendingScript)
                .withChangeAddress(sender2Addr);

        Result<String> result1 = quickTxBuilder.compose(scriptTx)
                .feePayer(sender2Addr)
                .withSigner(SignerProviders.signerFrom(sender1))
                .withSigner(SignerProviders.signerFrom(sender2))
                .mergeChangeOutputs(false)
                .completeAndWait(System.out::println);

        System.out.println(result1.getResponse());
        assertTrue(result1.isSuccessful());
    }

    @Test
    void alwaysTrueScript_minting_multipleReceiverNewTokens() throws ApiException, CborSerializationException, InterruptedException {
        PlutusV2Script mintingScript = PlutusV2Script.builder()
                .type("PlutusScriptV2")
                .cborHex("49480100002221200101")
                .build();

        PlutusV2Script spendingScript = PlutusV2Script.builder()
                .type("PlutusScriptV2")
                .cborHex("49480100002221200101")
                .build();

        String scriptAddress = AddressProvider.getEntAddress(spendingScript, Networks.testnet()).toBech32();
        BigInteger scriptAmt = new BigInteger("2479280");

        Random rand = new Random();
        int randInt = rand.nextInt();
        BigIntPlutusData plutusData =  new BigIntPlutusData(BigInteger.valueOf(randInt)); //any random number

        Tx tx = new Tx();
        tx.payToContract(scriptAddress, Amount.lovelace(scriptAmt), plutusData)
                .from(sender2Addr);

        QuickTxBuilder quickTxBuilder = new QuickTxBuilder(backendService);
        Result<String> result = quickTxBuilder.compose(tx)
                .withSigner(SignerProviders.signerFrom(sender2))
                .completeAndWait(System.out::println);

        System.out.println(result.getResponse());

        //Required as backend service returns outdated utxo
        if (result.isSuccessful()) {
            checkIfUtxoAvailable(result.getValue(), sender2Addr);
        }

        System.out.println("Script Addr: " + scriptAddress);
        System.out.println("Datum: " + randInt);
        Optional<Utxo> spendingUtxoOptional  = ScriptUtxoFinders.findFirstByInlineDatum(utxoSupplier, scriptAddress, plutusData);

        Asset asset = new Asset("PlutusMintToken", BigInteger.valueOf(4000));
        String policyId = mintingScript.getPolicyId();

        ScriptTx scriptTx = new ScriptTx()
                .collectFrom(spendingUtxoOptional.get(), plutusData)
                .attachSpendingValidator(spendingScript)
                .payToAddress(receiver2, Amount.lovelace(scriptAmt))
                .mintAsset(mintingScript, asset, PlutusData.unit())
                .payToAddress(receiver1, Amount.asset(policyId, asset.getName(), BigInteger.valueOf(1000)), true)
                .payToAddress(receiver3, Amount.asset(policyId, asset.getName(), BigInteger.valueOf(2500)), true)
                .payToContract(scriptAddress, List.of(Amount.asset(policyId, asset.getName(), BigInteger.valueOf(500))), plutusData, true);

        Result<String> result1 = quickTxBuilder.compose(scriptTx)
                .feePayer(sender2Addr)
                .withSigner(SignerProviders.signerFrom(sender1))
                .withSigner(SignerProviders.signerFrom(sender2))
                .mergeChangeOutputs(false)
                .completeAndWait(System.out::println);

        System.out.println(result1.getResponse());
        assertTrue(result1.isSuccessful());
    }

    private void checkIfUtxoAvailable(String txHash, String address) {
        Optional<Utxo> utxo = Optional.empty();
        while (utxo.isEmpty()) {
            List<Utxo> utxos = utxoSupplier.getAll(address);
            utxo = utxos.stream().filter(u -> u.getTxHash().equals(txHash))
                    .findFirst();
            System.out.println("Try to get new output... txhash: " + txHash);
            try {
                Thread.sleep(1000);
            } catch (Exception e) {}
        }
    }

}
