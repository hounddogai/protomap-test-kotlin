package com.acme.transport.customs.declaration.overrideitem

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling declaration overrides by code. */
interface RollingDeclarationOverrideStore {
    fun find(code: String): RollingDeclarationOverride?
    fun save(item: RollingDeclarationOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingDeclarationOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingDeclarationOverrideStore(
    var items: MutableMap<String, RollingDeclarationOverride>,
    var order: MutableList<String>,
) : RollingDeclarationOverrideStore {
    override fun find(code: String): RollingDeclarationOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingDeclarationOverride) {
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

    override fun list(): MutableList<RollingDeclarationOverride> {
        val out = mutableListOf<RollingDeclarationOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingDeclarationOverrideStore(): RollingDeclarationOverrideStore {
    return MemoryRollingDeclarationOverrideStore(
        mutableMapOf<String, RollingDeclarationOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingDeclarationOverrideStore(
    var inner: RollingDeclarationOverrideStore,
    var cache: TtlCache<RollingDeclarationOverride>,
    var hits: Int,
    var misses: Int,
) : RollingDeclarationOverrideStore {
    override fun find(code: String): RollingDeclarationOverride? {
        val hit = (cache.get(code) as RollingDeclarationOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingDeclarationOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingDeclarationOverride> = inner.list()
}

fun newCachedRollingDeclarationOverrideStore(inner: RollingDeclarationOverrideStore, ttl: Int, clock: Clock): RollingDeclarationOverrideStore {
    return CachedRollingDeclarationOverrideStore(
        inner = inner,
        cache = newTtlCache<RollingDeclarationOverride>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling declaration overrides. Empty fields match all. */
data class RollingDeclarationOverrideQuery(
    var name: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingDeclarationOverride): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingDeclarationOverride>): MutableList<RollingDeclarationOverride> {
        val matched = filter(items) { candidate: RollingDeclarationOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingDeclarationOverrides(store: RollingDeclarationOverrideStore, query: RollingDeclarationOverrideQuery): MutableList<RollingDeclarationOverride> {
    return query.apply(store.list())
}
