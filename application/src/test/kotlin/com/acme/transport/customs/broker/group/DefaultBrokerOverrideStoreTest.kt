package com.acme.transport.customs.broker.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultBrokerOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultBrokerOverrideStore()
        val item = newDefaultBrokerOverride("code-778", "label-372")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
