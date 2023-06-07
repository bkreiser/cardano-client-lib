package com.bloxbean.cardano.client.crypto.cip1852;

import com.bloxbean.cardano.client.crypto.Bech32;
import com.bloxbean.cardano.client.crypto.bip32.HdKeyPair;
import com.bloxbean.cardano.client.crypto.bip32.key.HdPublicKey;
import com.bloxbean.cardano.client.util.HexUtil;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CIP1852Test {

    @Test
    void getPaymentVerificationKey() {
        String mnemonicPhrase = "indicate traffic belt syrup chief accident put upset present short drink bus glide warm roof";

        DerivationPath derivationPath = DerivationPath.builder()
                .purpose(new Segment(1852, true))
                .coinType(new Segment(1815, true))
                .account(new Segment(0, true))
                .role(new Segment(0, false))
                .index(new Segment(0, false))
                .build();

        HdKeyPair hdKeyPair = new CIP1852().getKeyPairFromMnemonic(mnemonicPhrase, derivationPath);
        byte[] pvtKeyBytes  = hdKeyPair.getPrivateKey().getBytes();
        byte[] publicKey  = hdKeyPair.getPublicKey().getBytes();

        String publicAdd = Bech32.encode(publicKey, "addr_xvk");
        assertThat(publicAdd).isEqualTo("addr_xvk1r30n0pv6d40kzzl4e6xje2y7c446gw2x9sgnms3vv62tx264tf5n9lxnuxqc5xpqlg30dtlq0tf0fav4kafsge6u24x296vg85l399cx2uv4k");
    }

    @Test
    void getStakeVerificationKey() {
        String mnemonicPhrase = "indicate traffic belt syrup chief accident put upset present short drink bus glide warm roof";

        DerivationPath derivationPath = DerivationPath.builder()
                .purpose(new Segment(1852, true))
                .coinType(new Segment(1815, true))
                .account(new Segment(0, true))
                .role(new Segment(2, false))
                .index(new Segment(0, false))
                .build();

        HdKeyPair hdKeyPair = new CIP1852().getKeyPairFromMnemonic(mnemonicPhrase, derivationPath);
        byte[] pvtKeyBytes  = hdKeyPair.getPrivateKey().getBytes();
        byte[] publicKey  = hdKeyPair.getPublicKey().getBytes();

        String publicAdd = Bech32.encode(publicKey, "stake_xvk");
        assertThat(publicAdd).isEqualTo("stake_xvk143rnqx89nnmlt8w5kerl03hvl2reuv02l450wjs2vd74cezqx2mja08euhtd7gejfylpfe8j3vgejh25nu9nwqgfx0qy8d40llf9h6qeg2t4z");

    }

    @Test
    void getPublicKeyFromAccountPubKey_acct_xvk() {
        String accountPubKey = "acct_xvk136qnzfm6c34lddfxll60uqxwe3csymp8sdqvg3zcs79azchhqdn4qhs75qtwvuzjkd5h436fujcrgqgq2mnlmr5yse5zvewdj6flkgg5catgs";
        Bech32.Bech32Data bech32Data = Bech32.decode(accountPubKey);
        String expectedStakePubKey = "03ab2b878c8f8ec759cee22c321f86270bea486796fb5b881241cfa9254a60f32ee31c3e3529c64f8c801fa5f630b52dd11491ded35d180325cd94a1f80f7f2f";
        HdPublicKey hdPublicKey = new CIP1852().getPublicKeyFromAccountPubKey(bech32Data.data, DerivationPath.createStakeAddressDerivationPath());

        assertThat(HexUtil.encodeHexString(hdPublicKey.getBytes())).isEqualTo(expectedStakePubKey);
    }

    @Test
    void getPublicKeyFromAccountPubKey_xpub() {
        String accountPubKey = "xpub136qnzfm6c34lddfxll60uqxwe3csymp8sdqvg3zcs79azchhqdn4qhs75qtwvuzjkd5h436fujcrgqgq2mnlmr5yse5zvewdj6flkgg88stt0";
        Bech32.Bech32Data bech32Data = Bech32.decode(accountPubKey);
        String expectedStakePubKey = "03ab2b878c8f8ec759cee22c321f86270bea486796fb5b881241cfa9254a60f32ee31c3e3529c64f8c801fa5f630b52dd11491ded35d180325cd94a1f80f7f2f";
        HdPublicKey hdPublicKey = new CIP1852().getPublicKeyFromAccountPubKey(bech32Data.data, DerivationPath.createStakeAddressDerivationPath());

        assertThat(HexUtil.encodeHexString(hdPublicKey.getBytes())).isEqualTo(expectedStakePubKey);
    }

    @Test
    void getPublicKeyFromAccountPubKey_acct_xvk_2() {
        String accountPubKey = "acct_xvk1zxnrf4j4xzvxwwkmsjsrvtgv6g5q4l9yyskp807d62w5y6zvnmhepfxyysq4nydjqsjxj2dcsfc6ns6ljm2gqs6jh5vj58auceyfadsydvkn7";
        Bech32.Bech32Data bech32Data = Bech32.decode(accountPubKey);
        String expectedStakePubKey = "23cebbe2b5707bb3f4255ca44398556925f3f6dc2b5f8c6f2f1b27f252dbbc12de7ea31330158d2b2f163c456416160ef8c8739a2336030f770314277fb1c9f8";
        HdPublicKey hdPublicKey = new CIP1852().getPublicKeyFromAccountPubKey(bech32Data.data, DerivationPath.createStakeAddressDerivationPath());

        assertThat(HexUtil.encodeHexString(hdPublicKey.getBytes())).isEqualTo(expectedStakePubKey);
    }

    @Test
    void getPublicKeyFromAccountPubKey_xpub_2() {
        String accountPubKey = "xpub1zxnrf4j4xzvxwwkmsjsrvtgv6g5q4l9yyskp807d62w5y6zvnmhepfxyysq4nydjqsjxj2dcsfc6ns6ljm2gqs6jh5vj58auceyfadshjpksp";
        Bech32.Bech32Data bech32Data = Bech32.decode(accountPubKey);
        String expectedStakePubKey = "23cebbe2b5707bb3f4255ca44398556925f3f6dc2b5f8c6f2f1b27f252dbbc12de7ea31330158d2b2f163c456416160ef8c8739a2336030f770314277fb1c9f8";
        HdPublicKey hdPublicKey = new CIP1852().getPublicKeyFromAccountPubKey(bech32Data.data, DerivationPath.createStakeAddressDerivationPath());

        assertThat(HexUtil.encodeHexString(hdPublicKey.getBytes())).isEqualTo(expectedStakePubKey);
    }
}
