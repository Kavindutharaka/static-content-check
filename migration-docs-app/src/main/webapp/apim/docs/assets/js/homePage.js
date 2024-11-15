// const baseURl = "http://localhost:8000/migration/docs";

// document.addEventListener("DOMContentLoaded", function () {
//   var path = window.location.pathname;

//   console.log(path);

// });

//   console.log("Dom Loaded");
//   console.log("path is " + path);



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


document.addEventListener('DOMContentLoaded', function() {

  var path = window.location.pathname;
  var navElement = document.getElementById('__nav_2_3_label');
  var navElement1 = document.getElementById('__nav_2_4_label');

  console.log("path is " + path);

  var pathTitle = "/identity-server/migration-docs/is-6.1.0/migrate-to-610.md"

  if (path.includes("is-6.1.0") && !path.includes("/is-6.1.0/what-has-changed/") || path.includes("/is-5.9.0/migrating-to-log4j2/")) {
    console.log("Hit Version: 6.1.0");
    if (navElement1) {
        navElement1.style.display = 'none';
    }
  
  }
  else if(path.includes("is-7.0.0") && !path.includes("/is-7.0.0/what-has-changed/") || path.includes("/is-5.10.0/migrating-to-log4j2/")){
    console.log("Hit Version: 7.0.0");
    if (navElement) {
        navElement.style.display = 'none';
    }
  }
    

  
});
