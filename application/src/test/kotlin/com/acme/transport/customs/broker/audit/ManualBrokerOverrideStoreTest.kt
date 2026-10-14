package com.acme.transport.customs.broker.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualBrokerOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualBrokerOverrideStore()
        val item = newManualBrokerOverride("code-723", "label-246")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
