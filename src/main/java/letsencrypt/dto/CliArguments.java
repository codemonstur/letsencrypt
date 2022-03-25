package letsencrypt.dto;

import jcli.annotations.CliCommand;
import jcli.annotations.CliOption;
import jcli.annotations.CliPositional;

import java.util.List;

@CliCommand(name = "letsencrypt", description = """
    The letsencrypt command allows for generating certificates using LetsEncrypt and several
    related features. The following commands exist:
    - account     : creates and account using the keypair for the certificate provider
    - certificate : requests a certificate for each domain given, uses DNS challenge
    - keypair     : create a cryptographic keypair
    - records     : list DNS records in godaddy""")
public final class CliArguments {

    @CliOption(longName = "staging", description = "Flag to use the staging server URI instead of the production URI")
    public boolean staging;

    @CliOption(name = 'd', longName = "domain", description = "A domain to request certificates for, repeatable")
    public List<String> domains;
    @CliOption(name = 'k', longName = "domain-keypair-file", defaultValue = "")
    public String domainKeyPairFile;
    @CliOption(name = 'a', longName = "account-keypair-file", defaultValue = "")
    public String accountKeyPairFile;
    @CliOption(name = 'o', longName = "output-file", defaultValue = "")
    public String outputFile;
    @CliOption(longName = "api-key", defaultValue = "", description = "The GoDaddy API key in [API_KEY]:[API_SECRET] format")
    public String godaddyApiKey;
    @CliOption(longName = "email", defaultValue = "jegvoorneveld@gmail.com")
    public String emailAddress;

    @CliPositional
    public Action action;

    @CliOption(name = 'h', longName = "help", isHelp = true)
    public boolean help;

}
