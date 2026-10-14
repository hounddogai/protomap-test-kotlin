package com.acme.transport.customs.declaration.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedDeclarationClaimTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedDeclarationClaim("code-858", "label-957")
        assertEquals(WeightedDeclarationClaimStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newWeightedDeclarationClaim("code-858", "label-957")
        try {
            item.moveTo(WeightedDeclarationClaimStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedDeclarationClaimStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedDeclarationClaim("code-858", "label-957")
        item.status = WeightedDeclarationClaimStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedDeclarationClaimStatus.PLANNED) }
    }
}
