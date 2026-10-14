package com.acme.transport.customs.declaration.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredDeclarationPolicyTest {
    @Test
    fun `starts in new`() {
        val item = newTieredDeclarationPolicy("code-690", "name-167")
        assertEquals(TieredDeclarationPolicyStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newTieredDeclarationPolicy("code-690", "name-167")
        try {
            item.moveTo(TieredDeclarationPolicyStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredDeclarationPolicyStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTieredDeclarationPolicy("code-690", "name-167")
        item.status = TieredDeclarationPolicyStatus.FAILED
        assertFails("move") { item.moveTo(TieredDeclarationPolicyStatus.NEW) }
    }
}
