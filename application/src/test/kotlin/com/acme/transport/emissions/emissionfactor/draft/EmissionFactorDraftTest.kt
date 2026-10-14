package com.acme.transport.emissions.emissionfactor.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorDraftTest {
    @Test
    fun `starts in new`() {
        val item = newEmissionFactorDraft("code-389", "label-343")
        assertEquals(EmissionFactorDraftStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newEmissionFactorDraft("code-389", "label-343")
        try {
            item.moveTo(EmissionFactorDraftStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorDraftStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newEmissionFactorDraft("code-389", "label-343")
        item.status = EmissionFactorDraftStatus.FAILED
        assertFails("move") { item.moveTo(EmissionFactorDraftStatus.NEW) }
    }
}
