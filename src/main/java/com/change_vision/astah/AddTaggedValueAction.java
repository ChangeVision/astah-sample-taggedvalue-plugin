package com.change_vision.astah;

import javax.swing.JOptionPane;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.BasicModelEditor;
import com.change_vision.jude.api.inf.editor.IModelEditorFactory;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.exception.ProjectNotFoundException;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IEntity;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.ui.IPluginActionDelegate;
import com.change_vision.jude.api.inf.ui.IWindow;
import com.change_vision.jude.api.inf.view.IProjectViewManager;
import com.change_vision.jude.api.inf.view.IViewManager;

public class AddTaggedValueAction implements IPluginActionDelegate {

    /**
     * <p>
     * 選択中のモデルにタグ付き値(キー:foo,値:var)を追加するメニューのためのアクションです。
     * <p>
     * the action of the menu to add tagged value for selecting model elements.
     * 
     * 
     * @param window astah*本体のwindow 
     *                the window of Astah
     */
    public Object run(IWindow window) throws UnExpectedException {
        AstahAPI api = getAstahAPI(window);
        ProjectAccessor projectAccessor = getProjectAccessor(window,api);
        BasicModelEditor basicModelEditor = getBasicModelEditor(window,projectAccessor);
        IViewManager viewManager = getViewManager(window,projectAccessor);
        IProjectViewManager projectViewManager = getProjectViewManager(viewManager);
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        
        createTaggedValueForSelectedEntities(basicModelEditor, transactionManager, projectViewManager);
        
        return null;
    }

    /**
     * AstahAPIを取得します。
     * 
     * @param window astah*本体のwindow。例外発生時にダイアログを出すために使用します。
     *                the window of Astah. it uses to show the alert dialog when exception is occurred.
     * @return AstahAPI
     * @throws UnExpectedException ここで例外が発生した場合は、実行環境等に異常があるので、例外を投げる
     */
    private AstahAPI getAstahAPI(IWindow window) throws UnExpectedException {
        AstahAPI api;
        try {
            api = AstahAPI.getAstahAPI();
        } catch (ClassNotFoundException e1) {
            JOptionPane.showMessageDialog(window.getParent(), "Unexpected error has occurred.",
                    "Alert", JOptionPane.ERROR_MESSAGE);
            throw new UnExpectedException();
        }
        return api;
    }

    /**
     * <P>
     * ProjectAccessorを取得します。
     * <P>
     * get ProjectAccessor
     * 
     * @param window astah*本体のwindow。例外発生時にダイアログを出すために使用します。
     *                the window of Astah. it uses to show the alert dialog when exception is occurred.
     * @param api AstahAPI
     * @return ProjectAccessor
     */
    private ProjectAccessor getProjectAccessor(IWindow window,AstahAPI api) {
        ProjectAccessor projectAccessor = api.getProjectAccessor();
        try {
            projectAccessor.getProject();
        } catch (ProjectNotFoundException e) {
            String message = "Project is not opened.Please open the project or create new project.";
            JOptionPane.showMessageDialog(window.getParent(), message, "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return projectAccessor;
    }

    /**
     * <p>
     * 基本的なモデルエディタを取得します。
     * <p>
     * get BasicModelEditor
     * 
     * @param window astah*本体のwindow。例外発生時にダイアログを出すために使用します。
     *                the window of Astah. it uses to show the alert dialog when exception is occurred.
     * @param projectAccessor ProjectAccessor
     * @return BasicModelEditor
     * @throws UnExpectedException
     */
    private BasicModelEditor getBasicModelEditor(IWindow window,ProjectAccessor projectAccessor)  throws UnExpectedException{
        BasicModelEditor basicModelEditor;
        try {
            basicModelEditor = getModelFactory(projectAccessor).getBasicModelEditor();
        } catch (InvalidEditingException e) {
                JOptionPane.showMessageDialog(window.getParent(), "Unexpected error has occurred.",
                        "Alert", JOptionPane.ERROR_MESSAGE);
                throw new UnExpectedException();
            }
        return basicModelEditor;
    }

    /**
     * <p>
     * モデルエディタ用のFactoryを取得します。
     * <p>
     * get the ModelEditorFactory
     * 
     * @param projectAccessor ProjectAccessor
     * @return IModelEditorFactory
     */
    private IModelEditorFactory getModelFactory(ProjectAccessor projectAccessor) {
        return projectAccessor.getModelEditorFactory();
    }

    /**
     * <p>
     * ViewManagerを取得します。
     * <p>
     * get ViewManager
     * 
     * @param window astah*本体のwindow。例外発生時にダイアログを出すために使用します。
     *                the window of Astah. it uses to show the alert dialog when exception is occurred.
     * @param projectAccessor ProjectAccessor
     * @return ViewManager
     * @throws UnExpectedException
     */
    private IViewManager getViewManager(IWindow window,ProjectAccessor projectAccessor) throws UnExpectedException{
        IViewManager viewManager = null;
        try {
            viewManager = projectAccessor.getViewManager();
        } catch (InvalidUsingException e) {
            JOptionPane.showMessageDialog(window.getParent(), "Unexpected error has occurred.",
                    "Alert", JOptionPane.ERROR_MESSAGE);
            throw new UnExpectedException();
        }
        return viewManager;
    }

    /**
     * <p>
     * 構造ツリー等のマネージャであるProjectViewManagerを取得します。
     * <p>
     * get ProjectViewManager, it manages Structured Tree, etc.
     * 
     * @param viewManager IViewManager
     * @return IProjectViewManager
     */
    private IProjectViewManager getProjectViewManager(IViewManager viewManager) {
        IProjectViewManager projectViewManager = viewManager.getProjectViewManager();
        return projectViewManager;
    }

    /**
     * <p>
     * 選択中のモデルにタグ付き値(キー:foo,値:var)を追加する実処理です。<br>
     * 構造ツリー上で選択しているEntityを取得・走査し、Elementかどうか確認後TaggedValueを設定します。
     * <p>
     * Main method of add tagged value to selecting model elements.<br>
     * Iterate the selection entities of the structured tree, and add tagged value when the object is element.
     * 
     * @param basicModelEditor   基本的なモデルのためのエディタ
     *                            the editor for basic model elements
     * @param transactionManager トランザクションマネージャ
     *                            the manager of transaction
     * @param entities           構造ツリーで選択中のEntity
     *                            the selection entities of structured tree
     */
    private void createTaggedValueForSelectedEntities(BasicModelEditor basicModelEditor,
            ITransactionManager transactionManager, IProjectViewManager projectViewManager) {
    
        IEntity[] entities = projectViewManager.getSelectedEntities();
        transactionManager.beginTransaction();
        try {
            for (IEntity entity : entities) {
                if (entity instanceof IElement) {
                    IElement element = (IElement) entity;
                    basicModelEditor.createTaggedValue(element, "foo", "var");
                }
            }
            transactionManager.endTransaction();
        } catch (Exception e) {
            transactionManager.abortTransaction();
        }
    }

}
