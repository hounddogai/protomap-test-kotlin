package com.acme.transport.customs.declaration.declarationwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackDeclarationBudgetTest {
    @Test
    fun `starts in requested`() {
        val item = newFallbackDeclarationBudget("code-245", "name-652")
        assertEquals(FallbackDeclarationBudgetStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newFallbackDeclarationBudget("code-245", "name-652")
        try {
            item.moveTo(FallbackDeclarationBudgetStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackDeclarationBudgetStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFallbackDeclarationBudget("code-245", "name-652")
        item.status = FallbackDeclarationBudgetStatus.EXPIRED
        assertFails("move") { item.moveTo(FallbackDeclarationBudgetStatus.REQUESTED) }
    }
}
