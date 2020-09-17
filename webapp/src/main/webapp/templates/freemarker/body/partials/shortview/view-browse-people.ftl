<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#-- Default individual browse view -->

<#import "lib-properties.ftl" as p>

<li class="individual" role="listitem" role="navigation">

<#if (individual.thumbUrl)??>
    <img src="${individual.thumbUrl}" width="90" alt="${individual.name}" />
    <h1 class="thumb">
        <a href="${individual.profileUrl}" title="${i18n().view_profile_page_for} ${individual.name}">${individual.name}</a>
    </h1>
<#else>
    <h1>
        <a href="${individual.profileUrl}" title="${i18n().view_profile_page_for} ${individual.name}">${individual.name}</a>
    </h1>
</#if>

<#if (extra[0].pt)?? >
    <span class="title">${extra[0].pt}</span>
<#else>
<#--HACK EHESS-->
    <#--<#assign cleanTypes = 'edu.cornell.mannlib.vitro.webapp.web.TemplateUtils$DropFromSequence'?new()(individual.mostSpecificTypes, vclass) />-->
    <#assign cleanTypes = individual.mostSpecificTypes />
    <#if cleanTypes?size == 1 && cleanTypes[0] != "Membre de l'établissement" && cleanTypes[0] != "Faculty Member">
        <span class="title">${cleanTypes[0]}</span>
    <#elseif (cleanTypes?size > 1) >
        <span class="title">
            <ul>
                <#list cleanTypes as type>
                    <#if type != "Membre de l'établissement" && type != "FacultyMember" >
                    <li>${type}</li>
                    </#if>
                <#--HACK EHESS END-->
                </#list>
            </ul>
        </span>
    </#if>
</#if>

</li>

