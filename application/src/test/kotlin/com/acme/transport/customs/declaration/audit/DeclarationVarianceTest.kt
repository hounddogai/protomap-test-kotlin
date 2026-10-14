package com.acme.transport.customs.declaration.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DeclarationVarianceTest {
    @Test
    fun `starts in requested`() {
        val item = newDeclarationVariance("code-463", "title-645")
        assertEquals(DeclarationVarianceStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newDeclarationVariance("code-463", "title-645")
        try {
            item.moveTo(DeclarationVarianceStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DeclarationVarianceStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDeclarationVariance("code-463", "title-645")
        item.status = DeclarationVarianceStatus.EXPIRED
        assertFails("move") { item.moveTo(DeclarationVarianceStatus.REQUESTED) }
    }
}
