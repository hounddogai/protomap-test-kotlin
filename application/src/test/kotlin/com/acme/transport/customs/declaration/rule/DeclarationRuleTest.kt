package com.acme.transport.customs.declaration.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DeclarationRuleTest {
    @Test
    fun `starts in idle`() {
        val item = newDeclarationRule("code-22", "name-943")
        assertEquals(DeclarationRuleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newDeclarationRule("code-22", "name-943")
        try {
            item.moveTo(DeclarationRuleStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DeclarationRuleStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDeclarationRule("code-22", "name-943")
        item.status = DeclarationRuleStatus.STOPPED
        assertFails("move") { item.moveTo(DeclarationRuleStatus.IDLE) }
    }
}
