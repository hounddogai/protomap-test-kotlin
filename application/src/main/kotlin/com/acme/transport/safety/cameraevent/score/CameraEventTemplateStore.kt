package com.acme.transport.safety.cameraevent.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event templates by code. */
interface CameraEventTemplateStore {
    fun find(code: String): CameraEventTemplate?
    fun save(item: CameraEventTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventTemplateStore(
    var items: MutableMap<String, CameraEventTemplate>,
    var order: MutableList<String>,
) : CameraEventTemplateStore {
    override fun find(code: String): CameraEventTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventTemplate) {
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

    override fun list(): MutableList<CameraEventTemplate> {
        val out = mutableListOf<CameraEventTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventTemplateStore(): CameraEventTemplateStore {
    return MemoryCameraEventTemplateStore(mutableMapOf<String, CameraEventTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventTemplateStore(
    var inner: CameraEventTemplateStore,
    var cache: TtlCache<CameraEventTemplate>,
    var hits: Int,
    var misses: Int,
) : CameraEventTemplateStore {
    override fun find(code: String): CameraEventTemplate? {
        val hit = (cache.get(code) as CameraEventTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventTemplate> = inner.list()
}

fun newCachedCameraEventTemplateStore(inner: CameraEventTemplateStore, ttl: Int, clock: Clock): CameraEventTemplateStore {
    return CachedCameraEventTemplateStore(
        inner = inner,
        cache = newTtlCache<CameraEventTemplate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event templates. Empty fields match all. */
data class CameraEventTemplateQuery(
    var country: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventTemplate): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CameraEventTemplate>): MutableList<CameraEventTemplate> {
        val matched = filter(items) { candidate: CameraEventTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventTemplates(store: CameraEventTemplateStore, query: CameraEventTemplateQuery): MutableList<CameraEventTemplate> {
    return query.apply(store.list())
}
