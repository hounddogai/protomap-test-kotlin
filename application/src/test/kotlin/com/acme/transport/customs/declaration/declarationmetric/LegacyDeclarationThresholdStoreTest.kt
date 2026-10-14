package com.acme.transport.customs.declaration.declarationmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyDeclarationThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyDeclarationThresholdStore()
        val item = newLegacyDeclarationThreshold("code-603", "title-55")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
