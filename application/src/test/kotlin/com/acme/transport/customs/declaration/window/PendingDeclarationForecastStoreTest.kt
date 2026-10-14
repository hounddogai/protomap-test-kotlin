package com.acme.transport.customs.declaration.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingDeclarationForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingDeclarationForecastStore()
        val item = newPendingDeclarationForecast("code-269", "title-655")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
