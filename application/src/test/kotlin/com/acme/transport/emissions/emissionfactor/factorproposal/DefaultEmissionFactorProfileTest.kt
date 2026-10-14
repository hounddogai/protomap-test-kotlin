package com.acme.transport.emissions.emissionfactor.factorproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultEmissionFactorProfileTest {
    @Test
    fun `starts in planned`() {
        val item = newDefaultEmissionFactorProfile("code-283", "title-909")
        assertEquals(DefaultEmissionFactorProfileStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newDefaultEmissionFactorProfile("code-283", "title-909")
        try {
            item.moveTo(DefaultEmissionFactorProfileStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultEmissionFactorProfileStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDefaultEmissionFactorProfile("code-283", "title-909")
        item.status = DefaultEmissionFactorProfileStatus.CANCELLED
        assertFails("move") { item.moveTo(DefaultEmissionFactorProfileStatus.PLANNED) }
    }
}
