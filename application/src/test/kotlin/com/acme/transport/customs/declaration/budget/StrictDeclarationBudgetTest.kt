package com.acme.transport.customs.declaration.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictDeclarationBudgetTest {
    @Test
    fun `starts in created`() {
        val item = newStrictDeclarationBudget("code-460", "label-778")
        assertEquals(StrictDeclarationBudgetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newStrictDeclarationBudget("code-460", "label-778")
        try {
            item.moveTo(StrictDeclarationBudgetStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictDeclarationBudgetStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newStrictDeclarationBudget("code-460", "label-778")
        item.status = StrictDeclarationBudgetStatus.RETIRED
        assertFails("move") { item.moveTo(StrictDeclarationBudgetStatus.CREATED) }
    }
}
