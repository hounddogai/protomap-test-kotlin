package com.acme.transport.kit.flags

import kotlin.test.Test
import kotlin.test.assertTrue

class FlagsTest {
    @Test
    fun `honors allow lists`() {
        val registry = newRegistry()
        registry.register(Flag("beta", true, AllowListStrategy(mutableListOf<String>("tenant-a"))))
        assertTrue(registry.enabled("beta", "tenant-a"), "tenant-a")
        assertTrue(!registry.enabled("beta", "tenant-b"), "tenant-b")
        assertTrue(!registry.enabled("missing", "tenant-a"), "missing flag")
    }
}
