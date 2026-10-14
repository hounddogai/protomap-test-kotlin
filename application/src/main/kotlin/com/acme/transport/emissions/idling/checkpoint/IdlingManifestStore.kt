package com.acme.transport.emissions.idling.checkpoint

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling manifests by code. */
interface IdlingManifestStore {
    fun find(code: String): IdlingManifest?
    fun save(item: IdlingManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingManifestStore(
    var items: MutableMap<String, IdlingManifest>,
    var order: MutableList<String>,
) : IdlingManifestStore {
    override fun find(code: String): IdlingManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingManifest) {
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

    override fun list(): MutableList<IdlingManifest> {
        val out = mutableListOf<IdlingManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingManifestStore(): IdlingManifestStore {
    return MemoryIdlingManifestStore(mutableMapOf<String, IdlingManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingManifestStore(
    var inner: IdlingManifestStore,
    var cache: TtlCache<IdlingManifest>,
    var hits: Int,
    var misses: Int,
) : IdlingManifestStore {
    override fun find(code: String): IdlingManifest? {
        val hit = (cache.get(code) as IdlingManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingManifest> = inner.list()
}

fun newCachedIdlingManifestStore(inner: IdlingManifestStore, ttl: Int, clock: Clock): IdlingManifestStore {
    return CachedIdlingManifestStore(
        inner = inner,
        cache = newTtlCache<IdlingManifest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling manifests. Empty fields match all. */
data class IdlingManifestQuery(
    var locale: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingManifest): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IdlingManifest>): MutableList<IdlingManifest> {
        val matched = filter(items) { candidate: IdlingManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingManifests(store: IdlingManifestStore, query: IdlingManifestQuery): MutableList<IdlingManifest> {
    return query.apply(store.list())
}
