---
title: "CITS3001 Hanabi Agent"
author: "Henry Hollingworth _21471423_"
date: "October 15, 2018"
link-citations: true
citation-style: https://raw.githubusercontent.com/citation-style-language/styles/master/apa.csl
classoption: article
header-includes:
    - \usepackage{multicol}
    - \usepackage{graphicx}
    - \usepackage{lipsum}
    - \newcommand{\hideFromPandoc}[1]{#1}
    - \hideFromPandoc{
        \let\Begin\begin
        \let\End\end
      }
...

\begin{multicols}{2}
    \lipsum[1-2]
\end{multicols}

```csharp
public class BankController : Controller {
    [Authenticate]
    [Route("accounts/details")]
    public Json getDetails([FromQuery]int user_id) {
        return Json(Database.getBankDetailsOfUser(query_id));
    }
}
```

\begin{multicols}{2}
    \lipsum[1-2]
\end{multicols}