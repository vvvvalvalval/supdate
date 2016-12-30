I experienced several approaches to make map updates as fast as possible:

1. using transients maps, from which the key would be accessed then updated
2. using a transient map `tm` and the original map `m`, the key being accessed from `m` and the update being written to `tm` (benchmarking having shown that access is much slower on transients)
3. not using transients at all

To my surprise, benchmarking showed that approach 3 was faster or no slower for various map sizes and key distributions.
