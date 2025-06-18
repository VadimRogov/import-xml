package import_xml.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageUtils {

    private final MessageSource messageSource;

    public String getMessage(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

    public String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }

    public String getImportStatusMessage(String status) {
        return getMessage("import.status." + status.toLowerCase());
    }

    public String getImportTypeMessage(String type) {
        return getMessage("import.type." + type.toLowerCase());
    }

    public String getErrorMessage(String code, Object... args) {
        return getMessage("error.import." + code, args);
    }

    public String getProgressMessage(String type, int processed, int failed) {
        return getMessage("progress.import.updated",
                getImportTypeMessage(type), processed, failed);
    }

    public String getValidationMessage(String type, String code, Object... args) {
        return getMessage("validation." + type + "." + code, args);
    }

    public String getLogMessage(String code, Object... args) {
        return getMessage("log.import." + code, args);
    }

    public String getCleanupMessage(String code, Object... args) {
        return getMessage("cleanup." + code, args);
    }

    public String getMonitoringMessage(String code, Object... args) {
        return getMessage("monitoring." + code, args);
    }
}