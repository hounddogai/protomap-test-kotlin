package com.acme.transport.customs.declaration.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DeclarationCycleTest {
    @Test
    fun `starts in planned`() {
        val item = newDeclarationCycle("code-710", "name-320")
        assertEquals(DeclarationCycleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newDeclarationCycle("code-710", "name-320")
        try {
            item.moveTo(DeclarationCycleStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DeclarationCycleStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDeclarationCycle("code-710", "name-320")
        item.status = DeclarationCycleStatus.CANCELLED
        assertFails("move") { item.moveTo(DeclarationCycleStatus.PLANNED) }
    }
}
