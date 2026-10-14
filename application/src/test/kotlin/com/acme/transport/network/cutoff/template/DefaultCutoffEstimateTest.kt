package com.acme.transport.network.cutoff.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultCutoffEstimateTest {
    @Test
    fun `starts in created`() {
        val item = newDefaultCutoffEstimate("code-656", "title-881")
        assertEquals(DefaultCutoffEstimateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newDefaultCutoffEstimate("code-656", "title-881")
        try {
            item.moveTo(DefaultCutoffEstimateStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultCutoffEstimateStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDefaultCutoffEstimate("code-656", "title-881")
        item.status = DefaultCutoffEstimateStatus.RETIRED
        assertFails("move") { item.moveTo(DefaultCutoffEstimateStatus.CREATED) }
    }
}
