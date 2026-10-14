package com.acme.transport.customs.declaration.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveDeclarationVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveDeclarationVarianceStore()
        val item = newAdaptiveDeclarationVariance("code-857", "name-37")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
