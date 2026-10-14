package com.acme.transport.customs.declaration.threshold

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration groups by code. */
interface DeclarationGroupStore {
    fun find(code: String): DeclarationGroup?
    fun save(item: DeclarationGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationGroupStore(
    var items: MutableMap<String, DeclarationGroup>,
    var order: MutableList<String>,
) : DeclarationGroupStore {
    override fun find(code: String): DeclarationGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationGroup) {
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

    override fun list(): MutableList<DeclarationGroup> {
        val out = mutableListOf<DeclarationGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationGroupStore(): DeclarationGroupStore {
    return MemoryDeclarationGroupStore(mutableMapOf<String, DeclarationGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationGroupStore(
    var inner: DeclarationGroupStore,
    var cache: TtlCache<DeclarationGroup>,
    var hits: Int,
    var misses: Int,
) : DeclarationGroupStore {
    override fun find(code: String): DeclarationGroup? {
        val hit = (cache.get(code) as DeclarationGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationGroup> = inner.list()
}

fun newCachedDeclarationGroupStore(inner: DeclarationGroupStore, ttl: Int, clock: Clock): DeclarationGroupStore {
    return CachedDeclarationGroupStore(
        inner = inner,
        cache = newTtlCache<DeclarationGroup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration groups. Empty fields match all. */
data class DeclarationGroupQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationGroup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<DeclarationGroup>): MutableList<DeclarationGroup> {
        val matched = filter(items) { candidate: DeclarationGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationGroups(store: DeclarationGroupStore, query: DeclarationGroupQuery): MutableList<DeclarationGroup> {
    return query.apply(store.list())
}
