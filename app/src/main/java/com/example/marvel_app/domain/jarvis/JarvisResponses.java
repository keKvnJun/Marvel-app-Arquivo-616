package com.example.marvel_app.domain.jarvis;

public final class JarvisResponses {
    private JarvisResponses() {
    }

    public static String forClassification(MarvelTopicClassifier.Result result) {
        if (result == MarvelTopicClassifier.Result.EMPTY) {
            return "Agente, informe uma consulta para que eu possa acessar os arquivos.";
        }
        if (result == MarvelTopicClassifier.Result.OUT_OF_SCOPE) {
            return "Agente, a S.H.I.E.L.D. classificou esta consulta como fora da missão. "
                    + "Meus protocolos atuais estão limitados ao universo Marvel.";
        }
        return "Consulta aceita. Acessando os arquivos autorizados da S.H.I.E.L.D.";
    }

    public static String noReliableData(String subject) {
        String safeSubject = subject == null || subject.trim().isEmpty()
                ? "esse assunto"
                : subject.trim();
        return "Não encontrei dados confiáveis sobre " + safeSubject
                + " nos arquivos disponíveis. Prefiro não improvisar informações, agente.";
    }

    public static String apiUnavailable() {
        return "A conexão segura com os arquivos foi interrompida. "
                + "Tente novamente quando o canal estiver disponível.";
    }

    public static String characterSummary(String name, String realName, String summary) {
        StringBuilder response = new StringBuilder("Dossiê localizado: ").append(name).append('.');
        if (realName != null && !realName.trim().isEmpty()) {
            response.append(" Identidade registrada: ").append(realName.trim()).append('.');
        }
        if (summary != null && !summary.trim().isEmpty()) {
            response.append(' ').append(summary.trim());
        }
        return response.toString();
    }
}
