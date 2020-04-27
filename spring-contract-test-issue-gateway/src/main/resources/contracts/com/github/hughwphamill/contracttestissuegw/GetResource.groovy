package com.github.hughwphamill.contracttestissuegw

import static org.springframework.cloud.contract.spec.Contract.make

[
        make {
            name("get_test")
            request {
                method 'GET'
                urlPath('/gwpath')
            }
            response {
                status OK()
                body(
                        output: 'hello'
                )
            }
        }
]

