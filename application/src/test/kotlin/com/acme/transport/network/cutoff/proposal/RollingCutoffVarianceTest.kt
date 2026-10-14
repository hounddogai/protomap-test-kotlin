package com.acme.transport.network.cutoff.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingCutoffVarianceTest {
    @Test
    fun `starts in created`() {
        val item = newRollingCutoffVariance("code-889", "name-307")
        assertEquals(RollingCutoffVarianceStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRollingCutoffVariance("code-889", "name-307")
        try {
            item.moveTo(RollingCutoffVarianceStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingCutoffVarianceStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRollingCutoffVariance("code-889", "name-307")
        item.status = RollingCutoffVarianceStatus.RETIRED
        assertFails("move") { item.moveTo(RollingCutoffVarianceStatus.CREATED) }
    }
}
