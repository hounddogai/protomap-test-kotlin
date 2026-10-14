package com.acme.transport.customs.declaration.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveDeclarationWindowTest {
    @Test
    fun `starts in created`() {
        val item = newAdaptiveDeclarationWindow("code-311", "label-326")
        assertEquals(AdaptiveDeclarationWindowStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newAdaptiveDeclarationWindow("code-311", "label-326")
        try {
            item.moveTo(AdaptiveDeclarationWindowStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveDeclarationWindowStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAdaptiveDeclarationWindow("code-311", "label-326")
        item.status = AdaptiveDeclarationWindowStatus.RETIRED
        assertFails("move") { item.moveTo(AdaptiveDeclarationWindowStatus.CREATED) }
    }
}
