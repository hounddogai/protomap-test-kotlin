package com.acme.transport.emissions.emissionfactor.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingEmissionFactorQuotaTest {
    @Test
    fun `starts in new`() {
        val item = newRollingEmissionFactorQuota("code-775", "name-269")
        assertEquals(RollingEmissionFactorQuotaStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newRollingEmissionFactorQuota("code-775", "name-269")
        try {
            item.moveTo(RollingEmissionFactorQuotaStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingEmissionFactorQuotaStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRollingEmissionFactorQuota("code-775", "name-269")
        item.status = RollingEmissionFactorQuotaStatus.FAILED
        assertFails("move") { item.moveTo(RollingEmissionFactorQuotaStatus.NEW) }
    }
}
