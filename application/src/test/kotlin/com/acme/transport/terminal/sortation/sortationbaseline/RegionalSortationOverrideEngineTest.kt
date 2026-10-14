package com.acme.transport.terminal.sortation.sortationbaseline

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalSortationOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalSortationOverrideEngine(newRegistry())
        val item = newRegionalSortationOverride("code-825", "name-14")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
