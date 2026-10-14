package com.acme.transport.network.servicearea.cappedareaassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryServiceAreaTargetTest {
    @Test
    fun `starts in proposed`() {
        val item = newPrimaryServiceAreaTarget("code-587", "title-640")
        assertEquals(PrimaryServiceAreaTargetStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newPrimaryServiceAreaTarget("code-587", "title-640")
        try {
            item.moveTo(PrimaryServiceAreaTargetStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryServiceAreaTargetStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPrimaryServiceAreaTarget("code-587", "title-640")
        item.status = PrimaryServiceAreaTargetStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PrimaryServiceAreaTargetStatus.PROPOSED) }
    }
}
