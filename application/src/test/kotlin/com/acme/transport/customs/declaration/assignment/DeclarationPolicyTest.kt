package com.acme.transport.customs.declaration.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DeclarationPolicyTest {
    @Test
    fun `starts in new`() {
        val item = newDeclarationPolicy("code-961", "label-819")
        assertEquals(DeclarationPolicyStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newDeclarationPolicy("code-961", "label-819")
        try {
            item.moveTo(DeclarationPolicyStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DeclarationPolicyStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDeclarationPolicy("code-961", "label-819")
        item.status = DeclarationPolicyStatus.FAILED
        assertFails("move") { item.moveTo(DeclarationPolicyStatus.NEW) }
    }
}
