package com.acme.transport.emissions.emissionfactor.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorAuditTest {
    @Test
    fun `starts in created`() {
        val item = newEmissionFactorAudit("code-645", "name-595")
        assertEquals(EmissionFactorAuditStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newEmissionFactorAudit("code-645", "name-595")
        try {
            item.moveTo(EmissionFactorAuditStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorAuditStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newEmissionFactorAudit("code-645", "name-595")
        item.status = EmissionFactorAuditStatus.RETIRED
        assertFails("move") { item.moveTo(EmissionFactorAuditStatus.CREATED) }
    }
}
