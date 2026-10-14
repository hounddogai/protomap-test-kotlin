package com.acme.transport.terminal.dockdoor.recorditem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientDockDoorClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientDockDoorClaimEngine(newRegistry())
        val item = newLenientDockDoorClaim("code-996", "name-428")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
