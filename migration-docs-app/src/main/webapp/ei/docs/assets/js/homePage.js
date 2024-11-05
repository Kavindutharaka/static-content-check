const baseURl = "http://localhost:8000/migration/docs";

// document.addEventListener("DOMContentLoaded", function () {
//   var path = window.location.pathname;

//   console.log("Dom Loaded");
//   console.log("path is " + path);
//   var dropdownContent = document.querySelector(".dropdown-content");

//   var chevronIcon = `
//     <span class="icon">
//       {% set chevron_icon = "fontawesome/solid/chevron-down" %}
//       {% include ".icons/" ~ chevron_icon ~ ".svg" %}
//     </span>
//   `;

//   var versionData = {
//     "enterprise-integrator": [
//       {
//         version: "ei-6.6.0",
//         path: `${baseURl}/enterprise-integrator/migration-docs/ei-6.6.0/upgrading-wso2-ei/`,
//       },
//       {
//         version: "ei-7.0.0",
//         path: `${baseURl}/enterprise-integrator/migration-docs/ei-7.0.0/upgrading-wso2-ei/`,
//       },
//       {
//         version: "ei-7.1.0",
//         path: `${baseURl}/enterprise-integrator/migration-docs/ei-7.1.0/upgrading-wso2-ei/`,
//       },
//     ],
//     "open-banking": [
//       {
//         version:"ob-1.5.0",
//         path: `${baseURl}/open-banking/migration-docs/ob-1.4.0-to-1.5.0/ReadMe/`
//       },
//       { version: "ob-2.0.0", 
//         path: `${baseURl}/open-banking/migration-docs/ob-1.5.0-to-2.0.0/ReadMe/`
//        },
//       {
//         version: "ob-3.0.0",
//         path: `${baseURl}/open-banking/migration-docs/ob-2.0.0-to-3.0.0/ReadMe/`,
//       },
     
//     ],
//     "api-manager" : [
//       {
//         version:"API-M 4.2.0",
//         path: `${baseURl}api-manager/apim-revamped/migration-catalog/upgrading-to-apim-420/upgrading-from-300-to-420/config-migration/`
//       },
//       {
//         version: "API-M 4.2.0",
//         path: `${baseURl}api-manager/apim-revamped/migration-catalog/upgrading-to-apim-420/upgrading-from-300-to-420/config-migration/`
//       },
//     ],
//   };

//   var versions = null;
//   var currentTitle = null;
//   if (path.includes("enterprise-integrator")) {
//     versions = versionData["enterprise-integrator"];
//     currentTitle = "Enterprise Integration";
//   } else if (path.includes("open-banking")) {
//     versions = versionData["open-banking"];
//     currentTitle = "Open Banking"; 
//   }
//   else if(path.includes("api-manager")){
//     versions = versionData["api-manager"];
//     currentTitle = "Api Manager"
//   }

//   if (versions, currentTitle) {

//     toggleNavItems(currentTitle);
//     dropdownContent.innerHTML = "";

//     versions.forEach(function (v) {
//       var li = document.createElement("li");
//       var a = document.createElement("a");
//       a.href = v.path;
//       a.target = "_blank";
//       a.textContent = v.version;
//       li.appendChild(a);
//       dropdownContent.appendChild(li);
//     });
//     console.log("Version Found");
    
//     document.getElementById("version-dropdown").style.display = "block";
//   } else {
//     console.log("Version not Found");
//     document.getElementById("version-dropdown").style.display = "none";
//   }

//   function toggleNavItems(title) {
//     console.log("toggleNavItem Function is working");
  
//     // Get the dropdown link element
//     const dropdownLink = document.querySelector("#version-dropdown");
  
//     // Update the dropdown text based on the title
//     if (title === "Enterprise Integration") {
//       dropdownLink.textContent = "Enterprise Integration";
//     } else if (title === "Open Banking") {
//       dropdownLink.textContent = "Open Banking";
//     }
//     else if(title === "Api Manager"){
//       dropdownLink.textContent = "Api Manager";
//     }
  
//     const navItems = document.querySelectorAll(".md-tabs__item");
//     navItems.forEach((item) => {
//       const link = item.querySelector(".md-tabs__link");
//       if (link) {
//         const linkText = link.textContent.trim();
//         if (
//           (title === "Enterprise Integration" &&
//             linkText !== "Home" &&
//             linkText !== "Enterprise Integration") ||
//           (title === "Open Banking" &&
//             linkText !== "Home" &&
//             linkText !== "Open Banking") ||
//             (title === "Api Manager" &&
//               linkText !== "Home" &&
//               linkText !== "Api Manager") 
//         ) {
//           item.style.display = "none";
//         } else if (title === "Home") {
//           item.style.display = "";
//         } else {
//           item.style.display = "";
//         }
//       }
//     });
//   }
  
  
// });

function navEnterpriseIntegrator() {

  window.open(`./enterprise-integrator/`, "_blank");

  console.log("Navigate To Enterprise Integrator Sucessfully");
}

function navOpenBank() {

  window.open(`./open-banking/`, "_blank");

  console.log("Navigate To Enterprise Integrator Sucessfully");
}

function navApi() {

  window.open(`./api-manager/`, "_blank");

  console.log("Navigate To Enterprise Integrator Sucessfully");
}
