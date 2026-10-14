package com.acme.transport.freightpay.accessorial.accessorialpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingAccessorialPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingAccessorialPolicyStore()
        val item = newRollingAccessorialPolicy("code-943", "label-235")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
