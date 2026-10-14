package com.acme.transport.lastmile.proofofdelivery.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultProofOfDeliveryTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultProofOfDeliveryTemplateStore()
        val item = newDefaultProofOfDeliveryTemplate("code-266", "name-822")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
