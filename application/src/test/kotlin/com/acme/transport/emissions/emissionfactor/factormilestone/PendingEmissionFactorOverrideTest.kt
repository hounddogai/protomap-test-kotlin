package com.acme.transport.emissions.emissionfactor.factormilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingEmissionFactorOverrideTest {
    @Test
    fun `starts in open`() {
        val item = newPendingEmissionFactorOverride("code-970", "name-531")
        assertEquals(PendingEmissionFactorOverrideStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newPendingEmissionFactorOverride("code-970", "name-531")
        try {
            item.moveTo(PendingEmissionFactorOverrideStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingEmissionFactorOverrideStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPendingEmissionFactorOverride("code-970", "name-531")
        item.status = PendingEmissionFactorOverrideStatus.ARCHIVED
        assertFails("move") { item.moveTo(PendingEmissionFactorOverrideStatus.OPEN) }
    }
}
