package com.acme.transport.customs.declaration.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingDeclarationVarianceTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingDeclarationVariance("code-382", "label-118")
        assertEquals(PendingDeclarationVarianceStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newPendingDeclarationVariance("code-382", "label-118")
        try {
            item.moveTo(PendingDeclarationVarianceStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingDeclarationVarianceStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingDeclarationVariance("code-382", "label-118")
        item.status = PendingDeclarationVarianceStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingDeclarationVarianceStatus.REQUESTED) }
    }
}
