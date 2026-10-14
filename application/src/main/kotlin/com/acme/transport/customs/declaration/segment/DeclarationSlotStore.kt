package com.acme.transport.customs.declaration.segment

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration slots by code. */
interface DeclarationSlotStore {
    fun find(code: String): DeclarationSlot?
    fun save(item: DeclarationSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationSlotStore(
    var items: MutableMap<String, DeclarationSlot>,
    var order: MutableList<String>,
) : DeclarationSlotStore {
    override fun find(code: String): DeclarationSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationSlot) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<DeclarationSlot> {
        val out = mutableListOf<DeclarationSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationSlotStore(): DeclarationSlotStore {
    return MemoryDeclarationSlotStore(mutableMapOf<String, DeclarationSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationSlotStore(
    var inner: DeclarationSlotStore,
    var cache: TtlCache<DeclarationSlot>,
    var hits: Int,
    var misses: Int,
) : DeclarationSlotStore {
    override fun find(code: String): DeclarationSlot? {
        val hit = (cache.get(code) as DeclarationSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationSlot> = inner.list()
}

fun newCachedDeclarationSlotStore(inner: DeclarationSlotStore, ttl: Int, clock: Clock): DeclarationSlotStore {
    return CachedDeclarationSlotStore(
        inner = inner,
        cache = newTtlCache<DeclarationSlot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration slots. Empty fields match all. */
data class DeclarationSlotQuery(
    var region: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationSlot): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DeclarationSlot>): MutableList<DeclarationSlot> {
        val matched = filter(items) { candidate: DeclarationSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationSlots(store: DeclarationSlotStore, query: DeclarationSlotQuery): MutableList<DeclarationSlot> {
    return query.apply(store.list())
}
