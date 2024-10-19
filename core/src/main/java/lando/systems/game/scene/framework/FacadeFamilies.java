package lando.systems.game.scene.framework;

import com.badlogic.gdx.utils.Array;

public interface FacadeFamilies {

    <F extends ComponentFamily> Array<F> getFamily(Class<F> clazz);

    <F extends ComponentFamily> Array<F> getFamily(int familyTypeId);
}
