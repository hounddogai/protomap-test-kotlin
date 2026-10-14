package com.acme.transport.customs.declaration.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualDeclarationAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualDeclarationAdjustmentStore()
        val item = newManualDeclarationAdjustment("code-352", "label-518")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
