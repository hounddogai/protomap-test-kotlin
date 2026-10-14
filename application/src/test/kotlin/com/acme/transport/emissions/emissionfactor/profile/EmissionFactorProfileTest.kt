package com.acme.transport.emissions.emissionfactor.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorProfileTest {
    @Test
    fun `starts in new`() {
        val item = newEmissionFactorProfile("code-395", "label-401")
        assertEquals(EmissionFactorProfileStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newEmissionFactorProfile("code-395", "label-401")
        try {
            item.moveTo(EmissionFactorProfileStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorProfileStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newEmissionFactorProfile("code-395", "label-401")
        item.status = EmissionFactorProfileStatus.FAILED
        assertFails("move") { item.moveTo(EmissionFactorProfileStatus.NEW) }
    }
}
