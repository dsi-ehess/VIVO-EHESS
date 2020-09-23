<#-- $This file is distributed under the terms of the license in LICENSE$  -->

<@widget name="login" include="assets" />

<!DOCTYPE html>
<html lang="${country}">
<head>
        <#include "head.ftl">
</head>

<body class="${bodyClasses!}">
<#list models?keys as key>
    <@listLabels key models[key] namesMap[key]/>
</#list>

<#macro listLabels title statements graph>
    <h1>${title}</h1>
<h2>(${graph})</h2>
<table style="width: 80%; margin: auto auto; border: thin solid 0xcc;">
        <#if statements?has_content>
            <#list statements as triple>
                <tr>
                    <td>&lt;${triple[0]}&gt;</td>
                    <td>&lt;${triple[1]}&gt;</td>
                    <td>&lt;${triple[2]}&gt;</td>
                    <td>
                        <form method="post" action="${urls.base}/i18n-cleaning">
                            <input type="hidden" name="graph" value="${graph}"/>
                            <input type="hidden" name="subject" value="${triple[0]}"/>
                            <input type="hidden" name="predicate" value="${triple[1]}"/>
                            <input type="hidden" name="object" value="${triple[2]}"/>
                            <input type="submit" value="X"/>
                        </form>
                    </td>
                </tr>
            </#list>
        <#else>
            <tr>
                <td>${i18n().none}</td>
            </tr>
        </#if>
</table>
</#macro>

<script>

</script>
</body>
</html>
