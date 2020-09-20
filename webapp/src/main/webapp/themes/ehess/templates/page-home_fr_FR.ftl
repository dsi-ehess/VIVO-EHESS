<#-- $This file is distributed under the terms of the license in LICENSE$  -->

<@widget name="login" include="assets" />

<#-- 
        With release 1.6, the home page no longer uses the "browse by" class group/classes display. 
        If you prefer to use the "browse by" display, replace the import statement below with the
        following include statement:
        
            <#include "browse-classgroups.ftl">
            
        Also ensure that the homePage.geoFocusMaps flag in the runtime.properties file is commented
        out.
-->
<#import "lib-home-page.ftl" as lh>

<!DOCTYPE html>
<html lang="${country}">
    <head>
        <#include "head.ftl">
        <#if geoFocusMapsEnabled >
            <#include "geoFocusMapScripts.ftl">
        </#if>
        <script async type="text/javascript" src="${urls.base}/js/homePageUtils.js?version=x"></script>
    </head>
    
    <body class="${bodyClasses!}" onload="${bodyOnload!}">
    <#-- supplies the faculty count to the js function that generates a random row number for the search query -->
        <@lh.facultyMemberCount  vClassGroups! />
        <#include "identity.ftl">

        <#include "menu.ftl">

    <div class="section-width">
        <div class="search-home-width">

                    <section id="search-home" role="region">
                        <h3>${i18n().intro_searchvivo} <span class="search-filter-selected">filteredSearch</span></h3>

                        <fieldset>
                            <legend>${i18n().search_form}</legend>
                            <form id="search-homepage" action="${urls.search}" name="search-home" role="search" method="post" >
                                <div id="search-home-field">
                                    <input type="text" name="querytext" class="search-homepage" value="" autocapitalize="off" />
                                    <input type="submit" value="${i18n().search_button}" class="search" />
                                    <input type="hidden" name="classgroup"  value="" autocapitalize="off" />
                                </div>

                                <a class="filter-search filter-default" href="#" title="${i18n().intro_filtersearch}">
                                    <span class="displace">${i18n().intro_filtersearch}</span>
                                </a>

                                <ul id="filter-search-nav">
                                    <li><a class="active" href="">${i18n().all_capitalized}</a></li>
                                    <@lh.allClassGroupNames vClassGroups! />
                                </ul>
                            </form>
                        </fieldset>
                    </section> <!-- #search-home -->
        </div>
    </div>
<div class="section-width bg-intro">
        <section id="intro" role="region">
            <div class="txt-intro">
                <h2>${i18n().intro_title}</h2>

                <p>${i18n().intro_para1}</p>
                <p>${i18n().intro_para2}</p>
                <p>${i18n().intro_para3}</p>
            </div>
            <div class="laptop-perspective">

                <!--<div class="laptop-perspective w-embed"><video muted="" autoplay="" loop="" class="laptop-perspective" poster="https://assets-global.website-files.com/5887c41397719f6a6e90971b/58a24a18300755f20f856b2f_laptop-big-save.jpg">
                        <source src="https://assets-global.website-files.com/5887c41397719f6a6e90971b/588ba4f9ac7ca67606b8d9cb_laptop-big%20(Converted)-transcode.webm" type="video/webm">
                        <source src="https://assets-global.website-files.com/5887c41397719f6a6e90971b/588ba4f9ac7ca67606b8d9cb_laptop-big%20(Converted)-transcode.mp4" type="video/mp4">
                        <img src="https://assets-global.website-files.com/5887c41397719f6a6e90971b/58a24a18300755f20f856b2f_laptop-big-save.jpg">
                    </video></div>-->
            </div>
        </section> <!-- #intro -->
</div>

        <!-- List of research classes: e.g., articles, books, collections, conference papers -->
        <@lh.researchClasses />
                
        <!-- List of four randomly selected faculty members -->
        <@lh.facultyMbrHtml />

        <!-- List of randomly selected academic departments -->
        <@lh.academicDeptsHtml />

        <#if geoFocusMapsEnabled >
            <!-- Map display of researchers' areas of geographic focus. Must be enabled in runtime.properties -->
            <@lh.geographicFocusHtml />
        </#if>

        <!-- Statistical information relating to property groups and their classes; displayed horizontally, not vertically-->
        <@lh.allClassGroups vClassGroups! />
        <#include "footer.ftl">
        <#-- builds a json object that is used by js to render the academic departments section -->
        <@lh.listAcademicDepartments />
    <script>       
        var i18nStrings = {
            researcherString: "${i18n().researcher?js_string}",
            researchersString: "${i18n().researchers?js_string}",
            currentlyNoResearchers: "${i18n().currently_no_researchers?js_string}",
            countriesAndRegions: "${i18n().countries_and_regions?js_string}",
            countriesString: "${i18n().countries?js_string}",
            regionsString: "${i18n().regions?js_string}",
            statesString: "${i18n().map_states_string?js_string}",
            stateString: "${i18n().map_state_string?js_string}",
            statewideLocations: "${i18n().statewide_locations?js_string}",
            researchersInString: "${i18n().researchers_in?js_string}",
            inString: "${i18n().in?js_string}",
            noFacultyFound: "${i18n().no_faculty_found?js_string}",
            placeholderImage: "${i18n().placeholder_image?js_string}",
            viewAllFaculty: "${i18n().view_all_faculty?js_string}",
            viewAllString: "${i18n().view_all?js_string}",
            viewAllDepartments: "${i18n().view_all_departments?js_string}",
            noDepartmentsFound: "${i18n().no_departments_found?js_string}"
        };
        // set the 'limmit search' text and alignment
        if  ( $('input.search-homepage').css('text-align') == "right" ) {       
             $('input.search-homepage').attr("value","${i18n().limit_search} \u2192");
        }  
    </script>
    </body>
</html>
