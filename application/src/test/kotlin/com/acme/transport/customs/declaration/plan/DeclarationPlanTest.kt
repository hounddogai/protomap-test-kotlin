package com.acme.transport.customs.declaration.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DeclarationPlanTest {
    @Test
    fun `starts in planned`() {
        val item = newDeclarationPlan("code-341", "name-599")
        assertEquals(DeclarationPlanStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newDeclarationPlan("code-341", "name-599")
        try {
            item.moveTo(DeclarationPlanStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DeclarationPlanStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDeclarationPlan("code-341", "name-599")
        item.status = DeclarationPlanStatus.CANCELLED
        assertFails("move") { item.moveTo(DeclarationPlanStatus.PLANNED) }
    }
}
