package com.acme.transport.kit.retry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RetryTest {
    @Test
    fun `stops at the first success`() {
        val policy = Policy(4, ConstantBackoff(10))
        val used = try {
            policy.run { attempt: Int -> attempt == 2 }
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(3, used, "attempts")
        assertEquals(40, policy.totalDelay(), "total delay")
    }
}
