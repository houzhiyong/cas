package org.apereo.cas.adaptors.yubikey.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link JsonYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class JsonYubiKeyAccountRegistry extends WhitelistYubiKeyAccountRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonYubiKeyAccountRegistry.class);
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
    private final Resource jsonResource;

    public JsonYubiKeyAccountRegistry(final Resource jsonResource, final YubiKeyAccountValidator validator) {
        super(getDevicesFromJsonResource(jsonResource), validator);
        this.jsonResource = jsonResource;
    }

    @Override
    public boolean registerAccount(final String uid, final String yubikeyPublicId) {
        try {
            final File file = jsonResource.getFile();
            this.devices.put(uid, yubikeyPublicId);
            MAPPER.writer().withDefaultPrettyPrinter().writeValue(file, this.devices);
            return true;
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private static Map<String, String> getDevicesFromJsonResource(final Resource jsonResource) {
        try {
            if (!ResourceUtils.doesResourceExist(jsonResource)) {
                final boolean res = jsonResource.getFile().createNewFile();
                if (!res) {
                    LOGGER.warn("Could not auto-create JSON resource @ [{}]", jsonResource);
                }
            }
            if (ResourceUtils.doesResourceExist(jsonResource)) {
                final File file = jsonResource.getFile();
                if (file.canRead() && file.length() > 0) {
                    return MAPPER.readValue(file, Map.class);
                }
            } else {
                LOGGER.warn("JSON resource @ [{}] does not exist", jsonResource);
            }
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
        return new HashMap<>();
    }
}
