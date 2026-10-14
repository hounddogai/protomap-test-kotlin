package com.acme.transport.emissions.emissionfactor.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorBundleTest {
    @Test
    fun `starts in open`() {
        val item = newEmissionFactorBundle("code-91", "title-102")
        assertEquals(EmissionFactorBundleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newEmissionFactorBundle("code-91", "title-102")
        try {
            item.moveTo(EmissionFactorBundleStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorBundleStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newEmissionFactorBundle("code-91", "title-102")
        item.status = EmissionFactorBundleStatus.ARCHIVED
        assertFails("move") { item.moveTo(EmissionFactorBundleStatus.OPEN) }
    }
}
