package com.acme.transport.linehaul.backhaul.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BackhaulEstimateTest {
    @Test
    fun `starts in requested`() {
        val item = newBackhaulEstimate("code-743", "name-483")
        assertEquals(BackhaulEstimateStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newBackhaulEstimate("code-743", "name-483")
        try {
            item.moveTo(BackhaulEstimateStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BackhaulEstimateStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newBackhaulEstimate("code-743", "name-483")
        item.status = BackhaulEstimateStatus.EXPIRED
        assertFails("move") { item.moveTo(BackhaulEstimateStatus.REQUESTED) }
    }
}
