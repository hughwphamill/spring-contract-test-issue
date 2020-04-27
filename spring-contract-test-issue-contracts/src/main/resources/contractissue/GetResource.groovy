package contractissue

import static org.springframework.cloud.contract.spec.Contract.make

[
        make {
            name("get_test")
            request {
                method 'GET'
                urlPath('/test/resource')
            }
            response {
                status OK()
                body(
                        output: 'hello'
                )
            }
        }
]

