package com.acme.transport.terminal.dockdoor.doorclaim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalDockDoorClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalDockDoorClaimEngine(newRegistry())
        val item = newRegionalDockDoorClaim("code-756", "name-817")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
